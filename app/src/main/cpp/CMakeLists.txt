cmake_minimum_required(VERSION 3.22.1)
project("camb_shell")

set(CMAKE_CXX_STANDARD 17)
add_library(camb_shell SHARED jni_bridge.cpp)

# 预编译so存放目录
set(PREBUILT_SO ${CMAKE_CURRENT_SOURCE_DIR}/prebuilt/arm64-v8a/libcamb_media.so)
add_library(libcamb_media SHARED IMPORTED)
set_target_properties(libcamb_media PROPERTIES
    IMPORTED_LOCATION ${PREBUILT_SO}
)

find_library(android-lib android)
find_library(log-lib log)

target_link_libraries(
    camb_shell
    libcamb_media
    ${android-lib}
    ${log-lib}
)
