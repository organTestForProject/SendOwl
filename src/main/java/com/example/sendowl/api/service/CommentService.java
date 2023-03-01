package com.example.sendowl.api.service;

import com.example.sendowl.domain.board.exception.enums.BoardErrorCode;
import com.example.sendowl.domain.board.entity.Board;
import com.example.sendowl.domain.comment.entity.Comment;
import com.example.sendowl.domain.comment.exception.CommentNoPermission;
import com.example.sendowl.domain.comment.exception.CommentNotFoundException;
import com.example.sendowl.domain.comment.exception.enums.CommentErrorCode;
import com.example.sendowl.domain.user.entity.User;
import com.example.sendowl.domain.board.exception.BoardNotFoundException;
import com.example.sendowl.domain.board.repository.BoardRepository;
import com.example.sendowl.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.sendowl.domain.comment.dto.CommentDto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public CommentRes insertComment(CommentReq vo, User user) {

        Board board = boardRepository.findById(vo.getBoardId())
                .orElseThrow(()->new BoardNotFoundException(BoardErrorCode.NOT_FOUND));

        Comment comment;

        if(vo.getParentId() != null){

            comment = Comment.builder()
                    .board(board)
                    .user(user)
                    .content(vo.getContent())
                    .parent(vo.getParentId())
                    .depth(1L)
                    .build();
        } else {
            comment = Comment.builder()
                    .board(board)
                    .user(user)
                    .content(vo.getContent())
                    .parent(null)
                    .depth(0L)
                    .build();
        }

        Comment savedComment = commentRepository.save(comment);

        return new CommentRes(savedComment);
    }

    public Page<CommentRes> selectCommentList(Long boardId, Pageable pageable){
        // 게시글 존재여부 확인
        Board board = boardRepository.findById(boardId).orElseThrow(
                ()-> new BoardNotFoundException(BoardErrorCode.NOT_FOUND));

        // 댓글 select
        Page<Comment> comments = commentRepository.findAllByBoard(board, pageable).orElseThrow(
                ()-> new BoardNotFoundException(CommentErrorCode.NOT_FOUND));

        // 가져온 Comment들의 Id를 담습니다.
        List<Long> commentIdList =
                comments.stream().map(Comment::getId).collect(Collectors.toList());

        // comments들의 대댓글들을 가져 옵니다.
        List<DtoInterface> childList = commentRepository.findAllChildComment(commentIdList);

        // comments <-> childList mapping by child's parent_id
        Map<String, List<CommentRes>> parentChildMap = new HashMap<>();
        for(DtoInterface crs : childList) {
            if(!parentChildMap.containsKey(crs.getParentId().toString())){
                parentChildMap.put(crs.getParentId().toString(), new ArrayList<>());
            }
            parentChildMap.get(crs.getParentId().toString()).add(new CommentRes(crs));
        }

        // make List<CommentRes> from comment entity
        List<CommentRes> commentList = new ArrayList<>();
        for(Comment crs : comments) {
            CommentRes temp = new CommentRes(crs);

            List<CommentRes> tempChild = parentChildMap.getOrDefault(temp.getId().toString(), new ArrayList<>());

            temp.setChildren(tempChild);
            commentList.add(temp);
        }

        // CommentRes를 content로 가지고, comments의 page 정보를 담는 Page<>를 return 합니다.
        return new PageImpl<>(commentList,
                PageRequest.of(comments.getPageable().getPageNumber(),comments.getPageable().getPageSize()),
                comments.getTotalElements());
    }

    @Transactional
    public CommentRes updateComment(UpdateReq crq, User user) {

        Comment updComment = commentRepository.findById(crq.getCommentId()).orElseThrow(
                () -> new CommentNotFoundException(CommentErrorCode.NOT_FOUND));

        // 글쓴 유저가 맞는지 확인하기
        if(updComment.getUser().getId() == user.getId()){
            updComment.updateContent(crq.getContent());
            commentRepository.save(updComment);
            CommentRes commentRes = new CommentRes(updComment);
            return commentRes;
        }
        else{
            throw new CommentNoPermission(CommentErrorCode.NO_PERMISSON);
        }
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment delComment = commentRepository.findById(commentId).orElseThrow(
                () -> new CommentNotFoundException(CommentErrorCode.NOT_FOUND));

        if(delComment.getUser().getId() == user.getId()){
            delComment.delete();
            commentRepository.save(delComment);
        }
        else{
            throw new CommentNoPermission(CommentErrorCode.NO_PERMISSON);
        }
    }
}
