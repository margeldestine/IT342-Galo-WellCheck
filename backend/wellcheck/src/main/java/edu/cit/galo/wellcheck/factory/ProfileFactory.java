package edu.cit.galo.wellcheck.factory;

import edu.cit.galo.wellcheck.entity.User;

/**
 * Factory interface for creating user profiles
 * Implements Factory Pattern to eliminate code duplication in AuthService
 */
public interface ProfileFactory {
    /**
     * Creates and saves a profile for the given user
     * @param user The user entity
     * @param profileData Object containing profile-specific data (DTO)
     */
    void createAndSaveProfile(User user, Object profileData);
}