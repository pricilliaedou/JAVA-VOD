package fr.vod.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.vod.repository.VideoCommentRepository;

@Service
public class VideoCommentService {

	@Autowired
	VideoCommentRepository videoCommentRepository;
	
}
