package com.example.sendowl.domain.comment.repository;


import com.example.sendowl.domain.board.entity.Board;
import com.example.sendowl.domain.comment.dto.CommentDto;
import com.example.sendowl.domain.comment.entity.Comment;
import com.example.sendowl.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.user WHERE c.board=:board and c.depth=0 and c.isDeleted = false",
            countQuery = "SELECT COUNT(*) FROM Comment c where c.board =:board and c.depth=0 and c.isDeleted=false")
    Optional<Page<Comment>> findAllByBoard(Board board, Pageable pageable);

    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.user WHERE c.board=:board and c.depth=0 and c.isDeleted = false ORDER BY c.likeCount desc, c.regDate desc")
    Optional<List<Comment>> findAllByBoardOrderByLikeCountDesc(Board board, Pageable pageable);

    // 부모댓글에 따른 대댓글 전체 select
    @Query(value = "SELECT c.comment_id AS commentId,\n" +
            "            c.content,\n" +
            "            c.depth,\n" +
            "            c.parent_id AS parentId,\n" +
            "            u.user_id AS userId,\n" +
            "            u.name,\n" +
            "            u.nick_name AS nickName,\n" +
            "            u.role,\n" +
            "            u.mbti,\n" +
            "            u.age,\n" +
            "            u.gender,\n" +
            "            u.profile_image AS profileImage,\n" +
            "            c.reg_date AS regDate,\n" +
            "            c.mod_date AS modDate,\n" +
            "            cl.clc\n" +
            "    FROM comment c LEFT JOIN user u ON c.user_id = u.user_id\n" +
            "                   LEFT JOIN (SELECT comment_id, COUNT(*) as clc FROM comment_like group by comment_id) as cl on c.comment_id = cl.comment_id\n" +
            "            WHERE c.depth = 1 AND c.is_deleted = 'N' AND c.parent_id in :commentList",
            nativeQuery = true)
    List<CommentDto.DtoInterface> findAllChildComment(List<Long> commentList);

    // TODO: 런칭 이후진행
    // 초기 로딩시 로딩될 대댓글 갯수 제한 + 더보기 api (pagenation)
    @Query(value = "SELECT\n" +
            "*\n" +
            "FROM\n" +
            "(SELECT c.comment_id AS commentId,\n" +
            "        c.content,\n" +
            "        c.depth,\n" +
            "        c.parent_id AS parentId,\n" +
            "        u.user_id AS userId,\n" +
            "        u.name,\n" +
            "        u.nick_name AS nickName,\n" +
            "        u.role,\n" +
            "        u.mbti,\n" +
            "        u.age,\n" +
            "        u.gender,\n" +
            "        cl.clc AS likeCount,\n" +
            "        RANK() OVER (PARTITION BY c.parent_id ORDER BY c.reg_date desc) AS a\n" +
            "    FROM comment c LEFT JOIN user u ON c.user_id = u.user_id\n" +
            "                   LEFT JOIN (SELECT comment_id, COUNT(*) as clc FROM comment_like group by comment_id) as cl on c.comment_id = cl.comment_id\n" +
            "            WHERE c.depth = 1 AND c.parent_id in :commentList ) as rankrow\n" +
            "            WHERE rankrow.a <= 5",
            nativeQuery = true)
    List<CommentDto.DtoInterface> findChildComment(List<Long> commentList);

    Optional<Long> countByUserAndRegDateBetween(User user, LocalDateTime today, LocalDateTime tomorrow);
}

