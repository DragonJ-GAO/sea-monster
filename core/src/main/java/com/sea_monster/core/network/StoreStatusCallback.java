package com.sea_monster.core.network;

import com.sea_monster.core.resource.model.StoreStatus;

import java.io.File;

public interface StoreStatusCallback extends StatusCallback<StoreStatus>,RequestCallback<File> {
}
