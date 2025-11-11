package fr.vod.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscribeForm {


    private String email;
    private String password;
    private String lastName;
    private String firstName;
    private Character gender;
    private String phone;

}