package com.github.stevenkin.jim.router;

import com.github.stevenkin.serialize.Package;

import java.util.Set;

public interface RouteStrategy {
    Set<Server> choose(Set<Server> serverSet, Package pkg);
}
