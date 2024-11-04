/**
 * 
 */
package com.abimulia.hotel.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * 
 */
@Entity
@Table(name = "airbnb_user")
public class User extends AbstractAuditingEntity<Long> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 796173704622369334L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSequenceGenerator")
	@SequenceGenerator(name = "userSequenceGenerator", sequenceName = "user_generator", allocationSize = 1)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name= "first_name")
	private String firstName;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "image_url")
	private String imageUrl;
	
	@UuidGenerator
	@Column(name = "public_id", nullable = false)
	private UUID publicId;
	
	@ManyToMany
    @JoinTable(name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")})
    private Set<Authority> authorities = new HashSet<>();

	@Override
	public Long getId() {
		return id;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the imageUrl
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	/**
	 * @param imageUrl the imageUrl to set
	 */
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	/**
	 * @return the publicId
	 */
	public UUID getPublicId() {
		return publicId;
	}

	/**
	 * @param publicId the publicId to set
	 */
	public void setPublicId(UUID publicId) {
		this.publicId = publicId;
	}

	/**
	 * @return the authorities
	 */
	public Set<Authority> getAuthorities() {
		return authorities;
	}

	/**
	 * @param authorities the authorities to set
	 */
	public void setAuthorities(Set<Authority> authorities) {
		this.authorities = authorities;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, firstName, imageUrl, lastName, publicId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(email, other.email) && Objects.equals(firstName, other.firstName)
				&& Objects.equals(imageUrl, other.imageUrl) && Objects.equals(lastName, other.lastName)
				&& Objects.equals(publicId, other.publicId);
	}

	@Override
	public String toString() {
		return "User [lastName=" + lastName + ", firstName=" + firstName + ", email=" + email + ", imageUrl=" + imageUrl
				+ ", publicId=" + publicId + "]";
	}
	
	

}
