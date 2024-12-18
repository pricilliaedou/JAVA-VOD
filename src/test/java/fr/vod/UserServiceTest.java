package fr.vod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.vod.model.User;
import fr.vod.model.Video;
import fr.vod.repository.UserRepository;
import fr.vod.service.UserService;

public class UserServiceTest {
	
	
	
	private User mockUser;
	
	@BeforeEach
	public void setUp() {
		
		MockitoAnnotations.openMocks(this);
		
		mockUser=new User();
		
		Video mockVideo=new Video();
		Set<Video> videos=new HashSet<Video>();
		videos.add(mockVideo);
		
		mockUser.setEmail("lisa@lisa.fr");
		mockUser.setFirstName("lisa");
		mockUser.setId(0);
		mockUser.setLastName("adda");
		mockUser.setPassword("lisalisa");
		mockUser.setPhone("060000100");
		mockUser.setVideoLikes(videos);
	
	}
	@Test
	public void testGetUserById() {
		//assertEquals(1,1);
		//ici on affirme que 1=1. se sera vert au niveau du test j-unit
		//Si on met assertEquals(1,0); se sera faux donc rouge
		
		//when(userRepository.findById(10)).thenReturn(Optional.of(mockUser)); autre option
		
		Optional<User> optionalUser=Optional.of(mockUser);
		when(userRepository.findByEmailAndPassword("lisa@lisa.fr","lisalisa")).thenReturn(mockUser);
		
		User result = userService.get("lisa@lisa.fr","lisalisa");

        assertNotNull(result);
        assertEquals("lisa", result.getFirstName());
        assertEquals("lisa",result.getFirstName());
        
		}
	
	
	
	@Mock
	private UserRepository userRepository;
	
	@InjectMocks
	private UserService userService;

}


