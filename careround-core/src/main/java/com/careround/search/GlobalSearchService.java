package com.careround.search;

import com.careround.search.dto.GlobalSearchResponse;

public interface GlobalSearchService {

    GlobalSearchResponse search(String hospitalId, String query);
}
