package org.infinity.passport.repository;

import org.infinity.passport.domain.DictItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the DictItem entity.
 */
@Repository
public interface DictItemRepository extends MongoRepository<DictItem, String> {

    List<DictItem> findByDictCodeAndDictItemCode(String dictCode, String dictItemCode);
}
