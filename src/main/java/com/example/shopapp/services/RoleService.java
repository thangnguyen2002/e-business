package com.example.shopapp.services;

import com.example.shopapp.models.Role;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.services.interfaces.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    @Autowired
    private final RoleRepository roleRepository;
    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
