package net.croz.nrich.search.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.criteria.JoinType;
import java.util.function.Predicate;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SearchJoin<R> {

    private final String path;

    private String alias;

    private JoinType joinType;

    private Predicate<R> condition;

    public static <R> SearchJoin<R> innerJoin(final String path) {
        return new SearchJoin<>(path, path, JoinType.INNER, null);
    }

    public static <R> SearchJoin<R> leftJoin(final String path) {
        return new SearchJoin<>(path, path, JoinType.LEFT, null);
    }
}
