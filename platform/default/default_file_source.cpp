#include <mbgl/storage/default_file_source.hpp>
#include <mbgl/storage/asset_file_source.hpp>
#include <mbgl/storage/online_file_source.hpp>
#include <mbgl/storage/sqlite_cache.hpp>

#include <mbgl/platform/platform.hpp>
#include <mbgl/util/url.hpp>

#include "offline_file_source.hpp"

namespace {

const std::string assetProtocol = "asset://";

bool isAssetURL(const std::string& url) {
    return std::equal(assetProtocol.begin(), assetProtocol.end(), url.begin());
}

} // namespace

namespace mbgl {

class DefaultFileSource::Impl {
public:
    Impl(const std::string& cachePath, const std::string& assetRoot)
        : assetFileSource(assetRoot),
          cache(SharedSQLiteCache::get(cachePath)),
          onlineFileSource(cache.get()),
          offlineFileSource(&onlineFileSource)
    {
    }
    
    Impl(const std::string& cachePath, const std::string& assetRoot, const std::string& offlineDatabasePath)
    : assetFileSource(assetRoot),
    cache(SharedSQLiteCache::get(cachePath)),
    onlineFileSource(cache.get()),
    offlineFileSource(&onlineFileSource, offlineDatabasePath)
    {
    }

    AssetFileSource assetFileSource;
    std::shared_ptr<SQLiteCache> cache;
    OnlineFileSource onlineFileSource;
    OfflineFileSource offlineFileSource;
};

class StyleFileRequest : public FileRequest {
public:
    StyleFileRequest(const std::string& url, FileSource::Callback callback, DefaultFileSource::Impl& impl) {
        const mbgl::LatLngBounds dummy;
        offlineRequest = impl.offlineFileSource.beginDownloading(url, dummy, 0.0f, 0.0f, [&impl, url, callback, this] (Response response) {
            callback(response);
        });
    }
    
    std::unique_ptr<FileRequest> offlineRequest;
};
    
class DefaultFileRequest : public FileRequest {
public:
    DefaultFileRequest(const Resource& resource, FileSource::Callback callback, DefaultFileSource::Impl& impl) {
        offlineRequest = impl.offlineFileSource.request(resource, [&impl, resource, callback, this] (Response response) {
            if (response.error) {
                onlineRequest = impl.onlineFileSource.request(resource, callback);
            } else {
                callback(response);
            }
        });
    }
    
    std::unique_ptr<FileRequest> offlineRequest;
    std::unique_ptr<FileRequest> onlineRequest;
};

DefaultFileSource::DefaultFileSource(const std::string& cachePath, const std::string& assetRoot)
    : impl(std::make_unique<DefaultFileSource::Impl>(cachePath, assetRoot)) {
}

DefaultFileSource::DefaultFileSource(const std::string& cachePath, const std::string& assetRoot, const std::string& offlineDatabasePath)
    : impl(std::make_unique<DefaultFileSource::Impl>(cachePath, assetRoot, offlineDatabasePath)) {
}
    
DefaultFileSource::~DefaultFileSource() = default;

void DefaultFileSource::setAccessToken(const std::string& accessToken) {
    impl->onlineFileSource.setAccessToken(accessToken);
}

std::string DefaultFileSource::getAccessToken() const {
    return impl->onlineFileSource.getAccessToken();
}

void DefaultFileSource::setMaximumCacheSize(uint64_t size) {
    impl->cache->setMaximumCacheSize(size);
}

void DefaultFileSource::setMaximumCacheEntrySize(uint64_t size) {
    impl->cache->setMaximumCacheEntrySize(size);
}

std::unique_ptr<FileRequest> DefaultFileSource::request(const Resource& resource, Callback callback) {
    if (isAssetURL(resource.url)) {
        return impl->assetFileSource.request(resource, callback);
    } else {
        return impl->onlineFileSource.request(resource, callback);
    }
}
    
std::unique_ptr<FileRequest> DefaultFileSource::downloadStyle(const std::string &url, Callback callback) {
    return std::make_unique<StyleFileRequest>(url, callback, *impl);
}
    
} // namespace mbgl
