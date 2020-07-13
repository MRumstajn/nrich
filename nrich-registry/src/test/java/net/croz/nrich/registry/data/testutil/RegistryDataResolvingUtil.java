package net.croz.nrich.registry.data.testutil;

import net.croz.nrich.registry.data.stub.RegistryTestEmbeddedUserGroup;
import net.croz.nrich.registry.data.stub.RegistryTestEmbeddedUserGroupId;
import net.croz.nrich.registry.data.stub.RegistryTestEntityWithIdClass;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

public final class RegistryDataResolvingUtil {

    private RegistryDataResolvingUtil() {
    }

    public static RegistryTestEntityWithIdClass findRegistryTestEntityWithIdClass(final EntityManager entityManager, final RegistryTestEntityWithIdClass registryTestEntityWithIdClass) {
        try {
            return (RegistryTestEntityWithIdClass) entityManager.createQuery("from RegistryTestEntityWithIdClass where firstId = :firstId and secondId = :secondId")
                    .setParameter("firstId", registryTestEntityWithIdClass.getFirstId())
                    .setParameter("secondId", registryTestEntityWithIdClass.getSecondId())
                    .getSingleResult();
        }
        catch (final NoResultException ignored) {
            return null;
        }
    }

    public static RegistryTestEmbeddedUserGroup findRegistryTestEmbeddedUserGroup(final EntityManager entityManager, final RegistryTestEmbeddedUserGroupId groupId) {
        try {
            return (RegistryTestEmbeddedUserGroup) entityManager.createQuery("from RegistryTestEmbeddedUserGroup where userGroupId.user.id = :userId and userGroupId.group.id = :groupId")
                    .setParameter("userId", groupId.getUser().getId())
                    .setParameter("groupId", groupId.getGroup().getId())
                    .getSingleResult();
        }
        catch (final NoResultException ignored) {
            return null;
        }
    }
}