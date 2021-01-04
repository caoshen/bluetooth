# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

aux_source_directory(libopus/include INCLUDE)
aux_source_directory(libopus/celt CELT)
aux_source_directory(libopus/silk SILK)
aux_source_directory(libopus/silk/float SILK_FLOAT)
aux_source_directory(libopus/src SRC)

include_directories(
        ${INCLUDE}
        ${CELT}
        ${SILK}
        ${SILK_FLOAT}
        ${SRC})

add_library(
        # Sets the name of the library.
        opus

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        opus.cpp
        ${CERLT}
        ${SILK}
        ${SILK_FLOAT}
        ${SRC}
)

find_library(
        log-lib
        log)

find_library(android-lib
        android)

target_link_libraries(
        opus
        ${log-lib}
        ${android-lib}
)
add_definitions(-DOUTSIDE_SPEEX -DOPUS_BUILD -DSTDC_HEADERS -DVAR_ARRAYS)
