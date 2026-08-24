package com.iexceed.appzillonbanking.cagl.cob.repository.cus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObPhotoThumbnail;
import com.iexceed.appzillonbanking.cagl.cob.domain.cus.TbObPhotoThumbnailId;

@Repository
public interface TbObPhotoThumbnailRepository
        extends JpaRepository<TbObPhotoThumbnail, TbObPhotoThumbnailId> {

    List<TbObPhotoThumbnail> findByApplicationIdInAndDocuType(List<String> applicationIds, String docuType);
}
