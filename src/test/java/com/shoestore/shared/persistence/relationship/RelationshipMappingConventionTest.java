package com.shoestore.shared.persistence.relationship;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class RelationshipMappingConventionTest {

  @Test
  void shouldMapChildToParentAsLazyManyToOne() throws Exception {

    Field parentField = RelationshipChildTestEntity.class.getDeclaredField("parent");

    ManyToOne manyToOne = parentField.getAnnotation(ManyToOne.class);

    assertThat(manyToOne).isNotNull();
    assertThat(manyToOne.fetch()).isEqualTo(FetchType.LAZY);
    assertThat(manyToOne.optional()).isFalse();
    assertThat(manyToOne.cascade()).isEmpty();
  }

  @Test
  void shouldDefineExplicitJoinColumnAndForeignKey() throws Exception {

    Field parentField = RelationshipChildTestEntity.class.getDeclaredField("parent");

    JoinColumn joinColumn = parentField.getAnnotation(JoinColumn.class);

    assertThat(joinColumn).isNotNull();
    assertThat(joinColumn.name()).isEqualTo("parent_id");
    assertThat(joinColumn.nullable()).isFalse();
    assertThat(joinColumn.updatable()).isFalse();

    ForeignKey foreignKey = joinColumn.foreignKey();

    assertThat(foreignKey.name()).isEqualTo("fk_relationship_children_parent");
  }

  @Test
  void shouldMapParentCollectionAsLazyInverseSide() throws Exception {

    Field childrenField = RelationshipParentTestEntity.class.getDeclaredField("children");

    OneToMany oneToMany = childrenField.getAnnotation(OneToMany.class);

    assertThat(oneToMany).isNotNull();
    assertThat(oneToMany.mappedBy()).isEqualTo("parent");
    assertThat(oneToMany.fetch()).isEqualTo(FetchType.LAZY);
    assertThat(oneToMany.orphanRemoval()).isTrue();
  }

  @Test
  void shouldLimitCascadeToPersistAndMerge() throws Exception {

    Field childrenField = RelationshipParentTestEntity.class.getDeclaredField("children");

    OneToMany oneToMany = childrenField.getAnnotation(OneToMany.class);

    assertThat(Arrays.asList(oneToMany.cascade()))
        .containsExactlyInAnyOrder(CascadeType.PERSIST, CascadeType.MERGE)
        .doesNotContain(CascadeType.REMOVE, CascadeType.ALL);
  }
}
