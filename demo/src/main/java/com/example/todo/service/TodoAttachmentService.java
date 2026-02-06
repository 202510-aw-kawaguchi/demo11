package com.example.todo.service;

import com.example.todo.entity.TodoAttachment;
import com.example.todo.mapper.TodoAttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoAttachmentService {

    private final TodoAttachmentMapper attachmentMapper;
    private final FileStorageService fileStorageService;

    @Transactional(rollbackFor = Exception.class)
    public TodoAttachment addAttachment(Long todoId, MultipartFile file) throws IOException {
        var stored = fileStorageService.store(file);
        TodoAttachment attachment = new TodoAttachment();
        attachment.setTodoId(todoId);
        attachment.setOriginalName(stored.originalName());
        attachment.setStoredName(stored.storedName());
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachmentMapper.insert(attachment);
        return attachment;
    }

    @Transactional(readOnly = true)
    public List<TodoAttachment> findByTodoId(Long todoId) {
        return attachmentMapper.findByTodoId(todoId);
    }

    @Transactional(readOnly = true)
    public TodoAttachment findByIdAndTodoId(Long id, Long todoId) {
        return attachmentMapper.findByIdAndTodoId(id, todoId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(TodoAttachment attachment) throws IOException {
        attachmentMapper.deleteById(attachment.getId());
        fileStorageService.delete(attachment.getStoredName());
    }

    @Transactional(readOnly = true)
    public byte[] loadAttachmentBytes(TodoAttachment attachment) throws IOException {
        return fileStorageService.readAllBytes(attachment.getStoredName());
    }
}
