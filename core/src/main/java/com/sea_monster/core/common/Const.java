package com.sea_monster.core.common;


public class Const {
	public static class Parcel {
		public static final int EXIST_SEPARATOR = 1;
		public static final int NON_SEPARATOR = 0;
	}

	public static class SYS {
		public static final int WORK_QUEUE_MAX_COUNT = 25;
		public static final int MAX_THREAD_WORKER_COUNT = 3;
		public static final int DEF_THREAD_WORDER_COUNT = 2;
		public static final int CREATE_THREAD_TIME_SPAN = 1;
		public static final int HTTP_MAX_CONN_COUNT = 5;
        public static final int HTTP_MAX_ROUTE_COUNT = 5;
        public static final int HTTP_GPRS_TIMEOUT = 20000;
		public static final int HTTP_GPRS_SOCKET_TIMEOUT = 20000;
		public static final int HTTP_GPRS_RES_SOCKET_TIMEOUT = 40000;
		public static final int HTTP_GPRS_SPEED = 2;
		public static final int HTTP_WIFI_SPEED = 10;
		public static final int HTTP_WIFI_TIMEOUT = 20000;
		public static final int HTTP_WIFI_SOCKET_TIMEOUT = 20000;
		public static final int DEFAULT_IMAGE_SIZE = 50000;
	}

	public static class DEF {
		public static final String POST_FILE_NAME = "photo.jpg";
		public static final String POST_NAME = "FileData";

		public static final String THREAD_POOL_NAME = "Task #";
	}
}