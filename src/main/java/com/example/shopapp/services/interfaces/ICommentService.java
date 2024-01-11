package com.example.shopapp.services.interfaces;

import com.example.shopapp.dtos.CommentDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Comment;
import com.example.shopapp.responses.CommentResponse;

import java.util.List;

public interface ICommentService {
    Comment insertComment(CommentDTO commentDTO);
    void deleteComment(Long commentId);
    void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException;
    List<CommentResponse> getCommentsByUserAndProduct(Long userId, Long productId);
    List<CommentResponse> getCommentsByProduct(Long productId);
}
