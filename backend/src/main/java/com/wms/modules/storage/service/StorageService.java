package com.wms.modules.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.storage.entity.Storage;

import java.util.List;

public interface StorageService extends IService<Storage> {

    List<Storage> listStorages(String keyword, Long materialId, String batchNumber);

    Storage getStorageById(Long id);

    boolean saveStorage(Storage storage);

    boolean updateStorage(Storage storage);

    boolean removeStorage(Long id);

}
