package com.infopharma.ipos_sa.mapper;

/**
 * Mapper
 * Generic bidirectional mapper interface used throughout the service layer.
 * {@code mapTo(A)} converts an entity or source object to its DTO counterpart;
 * {@code mapFrom(B)} is the reverse direction (used less frequently).
 *
 * @param <A> source type (usually an entity)
 * @param <B> target type (usually a DTO or request object)
 */
public interface Mapper<A,B> {

    B mapTo(A a);

    A mapFrom(B b);

}