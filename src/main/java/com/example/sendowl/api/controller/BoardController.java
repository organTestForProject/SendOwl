package com.example.sendowl.api.controller;


import com.example.sendowl.domain.board.dto.BoardRequest;
import com.example.sendowl.domain.board.dto.BoardResponse;
import com.example.sendowl.domain.board.entity.Board;
import com.example.sendowl.api.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/boards")
public class BoardController {

    private final BoardService boardService;

    // 게시글 목록
    @Operation(summary = "list api", description = "list api")
    @GetMapping(path = "/")
    public ResponseEntity<List<Board>> boards(
            ){
        List<Board> boards = boardService.getBoardList();

        return new ResponseEntity<List<Board>>(boards, HttpStatus.OK);
    }

    // 게시글 상세
    @Operation(summary = "board api", description = "board api")
    @GetMapping(path = "/{id}") // join
    public ResponseEntity<BoardResponse> boardDetail(
            @PathVariable Long id
    ){
        Board boardDetail = boardService.getBoard(id);

        BoardResponse boardResponse = BoardResponse.builder()
                .title(boardDetail.getTitle())
                .content(boardDetail.getContent())
                .id(boardDetail.getId() + "")
                .regId(boardDetail.getUser().getId())
                .build();

        return new ResponseEntity<BoardResponse>(boardResponse, HttpStatus.OK);
    }

    // 게시글 등록
    @Operation(summary = "board insert api", description = "board insert api")
    @PostMapping(path = "/board")
    public ResponseEntity<Board> insertBoard(@RequestBody BoardRequest rq){
        boardService.insertBoard(rq);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

}
