package com.example.shopapp.services;

import com.example.shopapp.dtos.CommentDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Comment;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.CommentRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.responses.CommentResponse;
import com.example.shopapp.services.interfaces.ICommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService implements ICommentService {
    @Autowired
    private final CommentRepository commentRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ProductRepository productRepository;
    @Override
    @Transactional
    public Comment insertComment(CommentDTO commentDTO) {
        User user = userRepository.findById(commentDTO.getUserId()).orElse(null);
        Product product = productRepository.findById(commentDTO.getProductId()).orElse(null);
        if (user == null || product == null) {
            throw new IllegalArgumentException("User or product not found");
        }
        Comment newComment = Comment.builder()
                .user(user)
                .product(product)
                .content(commentDTO.getContent())
                .build();
        return commentRepository.save(newComment);
    }

    @Override
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException {
        Comment existingCommnet = commentRepository.findById(id)
                .orElseThrow(()->new DataNotFoundException("Comment not found"));
        existingCommnet.setContent(commentDTO.getContent());
        commentRepository.save(existingCommnet);
    }

    @Override
    public List<CommentResponse> getCommentsByUserAndProduct(Long userId, Long productId) {
        List<Comment> comments = commentRepository.findByUserIdAndProductId(userId,productId);
        return comments.stream().map(CommentResponse::fromComment).toList();
    }

    @Override
    public List<CommentResponse> getCommentsByProduct(Long productId) {
        List<Comment> comments = commentRepository.findByProductId(productId);
        return comments.stream().map(CommentResponse::fromComment).toList();
    }
}
