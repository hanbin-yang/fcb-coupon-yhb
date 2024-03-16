package com.fcb.coupon.backend.mongo;

import com.fcb.coupon.backend.model.mongo.MktTaskRunNodeUserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;


/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年08月23日 16:17:00
 */
public interface MktTaskRunNodeUserRepository extends MongoRepository<MktTaskRunNodeUserEntity, String> {


    List<MktTaskRunNodeUserEntity> findByMongoIdIn(Collection mongoIds);

    MktTaskRunNodeUserEntity findByMongoId(String mongoId);
}
