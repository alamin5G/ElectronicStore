package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.model.Address;
import com.goonok.electronicstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Finds all addresses associated with a specific user.
     * This is used internally by other service methods.
     *
     * @param user The user entity.
     * @return A list of addresses belonging to the user.
     */
    List<Address> findByUser(User user);

    /**
     * Finds all addresses for a given user, ordered primarily by the default shipping flag (true first),
     * and secondarily by the default billing flag (true first). Useful for displaying the address book
     * with default addresses prioritized.
     *
     * @param user The user entity.
     * @return A list of the user's addresses, ordered by default status.
     */
    List<Address> findByUserOrderByIsDefaultShippingDescIsDefaultBillingDesc(User user);

    /**
     * Finds a specific address by its ID, but only if it belongs to the specified user.
     * This prevents users from accessing or modifying addresses that aren't theirs.
     *
     * @param addressId The ID of the address to find.
     * @param user The user entity that should own the address.
     * @return An Optional containing the Address if found and owned by the user, otherwise empty.
     */
    Optional<Address> findByAddressIdAndUser(Long addressId, User user);

    // Add other custom queries if needed later
}
