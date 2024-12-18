package fr.vod.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.vod.repository.VideoRepository;

@Service
public class VideoService {

	@Autowired
	VideoRepository videoRepository;
	
}
