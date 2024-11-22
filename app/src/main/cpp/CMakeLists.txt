# CMakeLists.txt
cmake_minimum_required(VERSION 3.4.1)

add_library(
    signal_handler
    SHARED
    signal_handler.c
)

find_library(
    log-lib
    log
)

target_link_libraries(
    signal_handler
    ${log-lib}
)
