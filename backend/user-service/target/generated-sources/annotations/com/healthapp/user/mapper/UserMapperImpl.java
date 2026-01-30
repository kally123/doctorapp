package com.healthapp.user.mapper;

import com.healthapp.user.model.dto.RegisterRequest;
import com.healthapp.user.model.dto.UpdateProfileRequest;
import com.healthapp.user.model.dto.UserDto;
import com.healthapp.user.model.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-30T19:48:43+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.email( user.getEmail() );
        userDto.phone( user.getPhone() );
        userDto.role( user.getRole() );
        userDto.status( user.getStatus() );
        userDto.firstName( user.getFirstName() );
        userDto.lastName( user.getLastName() );
        userDto.displayName( user.getDisplayName() );
        userDto.avatarUrl( user.getAvatarUrl() );
        userDto.dateOfBirth( user.getDateOfBirth() );
        userDto.gender( user.getGender() );
        if ( user.getEmailVerified() != null ) {
            userDto.emailVerified( user.getEmailVerified() );
        }
        if ( user.getPhoneVerified() != null ) {
            userDto.phoneVerified( user.getPhoneVerified() );
        }
        userDto.createdAt( user.getCreatedAt() );
        userDto.lastLoginAt( user.getLastLoginAt() );

        userDto.id( user.getId().toString() );

        return userDto.build();
    }

    @Override
    public User toEntity(RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.getEmail() );
        user.phone( request.getPhone() );
        user.role( request.getRole() );
        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );

        return user.build();
    }

    @Override
    public void updateFromDto(UpdateProfileRequest request, User user) {
        if ( request == null ) {
            return;
        }

        if ( request.getFirstName() != null ) {
            user.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            user.setLastName( request.getLastName() );
        }
        if ( request.getDisplayName() != null ) {
            user.setDisplayName( request.getDisplayName() );
        }
        if ( request.getAvatarUrl() != null ) {
            user.setAvatarUrl( request.getAvatarUrl() );
        }
        if ( request.getDateOfBirth() != null ) {
            user.setDateOfBirth( request.getDateOfBirth() );
        }
        if ( request.getGender() != null ) {
            user.setGender( request.getGender() );
        }
    }
}
