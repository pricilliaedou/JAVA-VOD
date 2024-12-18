package fr.vod.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="category")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	@Column(name="name")
	String name;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="category")
	Set<Video> videos;
	
	
}
