package com.example.shopapp.controllers;

import com.example.shopapp.dtos.CommentDTO;
import com.example.shopapp.dtos.CustomUserDetail;
import com.example.shopapp.models.Comment;
import com.example.shopapp.models.User;
import com.example.shopapp.responses.CommentResponse;
import com.example.shopapp.services.interfaces.ICommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/comments")
//@Validated
//Dependency Injection
@RequiredArgsConstructor
public class CommentController {
    private final ICommentService iCommentService;

    @PostMapping
    public ResponseEntity<?> insertComment(
            @Valid @RequestBody CommentDTO commentDTO
    ) {
        try {
            CustomUserDetail loginUser = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!Objects.equals(loginUser.getUserId(), commentDTO.getUserId())) {
                return new ResponseEntity<>("You cannot update another user's comment", HttpStatus.BAD_REQUEST);
            }
            Comment comment = iCommentService.insertComment(commentDTO);
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during comment insertion.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllComments(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam("product_id") Long productId
    ) {
        try {
            List<CommentResponse> commentResponses;
            if (userId == null) {
                commentResponses = iCommentService.getCommentsByProduct(productId);
            } else {
                commentResponses = iCommentService.getCommentsByUserAndProduct(userId, productId);
            }
            return new ResponseEntity<>(commentResponses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable("id") Long commentId,
            @Valid @RequestBody CommentDTO commentDTO
    ) {
        try {
            CustomUserDetail loginUser = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!Objects.equals(loginUser.getUserId(), commentDTO.getUserId())) {
                return new ResponseEntity<>("You cannot update another user's comment", HttpStatus.BAD_REQUEST);
            }
            iCommentService.updateComment(commentId, commentDTO);
            return new ResponseEntity<>("Update comment successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during comment update.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("id") Long commentId
    ) {
        try {
            iCommentService.deleteComment(commentId);
            return new ResponseEntity<>("Delete comment successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred during comment delete.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
