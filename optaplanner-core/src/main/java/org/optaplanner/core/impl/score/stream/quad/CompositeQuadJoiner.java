/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.score.stream.quad;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public final class CompositeQuadJoiner<A, B, C, D> extends AbstractQuadJoiner<A, B, C, D> {

    private final List<SingleQuadJoiner<A, B, C, D>> joinerList;
    private final TriFunction<A, B, C, ?>[] leftMappings;
    private final Function<D, ?>[] rightMappings;

    public CompositeQuadJoiner(List<SingleQuadJoiner<A, B, C, D>> joinerList) {
        if (joinerList.isEmpty()) {
            throw new IllegalArgumentException("The joinerList (" + joinerList + ") must not be empty.");
        }
        this.joinerList = joinerList;
        this.leftMappings = joinerList.stream()
                .map(SingleQuadJoiner::getLeftMapping)
                .toArray(TriFunction[]::new);
        this.rightMappings = joinerList.stream()
                .map(SingleQuadJoiner::getRightMapping)
                .toArray(Function[]::new);
    }

    public List<SingleQuadJoiner<A, B, C, D>> getJoinerList() {
        return joinerList;
    }

    // ************************************************************************
    // Builders
    // ************************************************************************

    @Override
    public TriFunction<A, B, C, Object> getLeftMapping(int joinerId) {
        return (TriFunction<A, B, C, Object>) leftMappings[joinerId];
    }

    @Override
    public TriFunction<A, B, C, Object[]> getLeftCombinedMapping() {
        final TriFunction<A, B, C, Object>[] mappings = IntStream.range(0, joinerList.size())
                .mapToObj(this::getLeftMapping)
                .toArray(TriFunction[]::new);
        return (A a, B b, C c) -> Arrays.stream(mappings)
                .map(f -> f.apply(a, b, c))
                .toArray();
    }

    @Override
    public JoinerType[] getJoinerTypes() {
        return joinerList.stream()
                .map(SingleQuadJoiner::getJoinerType)
                .toArray(JoinerType[]::new);
    }

    @Override
    public Function<D, Object> getRightMapping(int joinerId) {
        return (Function<D, Object>) rightMappings[joinerId];
    }

    @Override
    public Function<D, Object[]> getRightCombinedMapping() {
        final Function<D, Object>[] mappings = IntStream.range(0, joinerList.size())
                .mapToObj(this::getRightMapping)
                .toArray(Function[]::new);
        return (D d) -> Arrays.stream(mappings)
                .map(f -> f.apply(d))
                .toArray();
    }

}
