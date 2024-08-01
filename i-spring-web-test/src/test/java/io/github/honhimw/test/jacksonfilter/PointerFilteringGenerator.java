package io.github.honhimw.test.jacksonfilter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.filter.FilteringGeneratorDelegate;
import com.fasterxml.jackson.core.filter.TokenFilter;

import java.io.IOException;

/**
 * @author hon_him
 * @since 2023-05-16
 */

public class PointerFilteringGenerator extends FilteringGeneratorDelegate {

    public PointerFilteringGenerator(JsonGenerator d, TokenFilter f, TokenFilter.Inclusion inclusion, boolean allowMultipleMatches) {
        super(d, f, inclusion, allowMultipleMatches);
    }

    @Override
    public void writeStartArray(Object forValue) throws IOException
    {
        if (_itemFilter == null) {
            _filterContext = _filterContext.createChildArrayContext(null, false);
            return;
        }
        if (_itemFilter == TokenFilter.INCLUDE_ALL) {
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, true);
            delegate.writeStartArray(forValue);
            return;
        }
        _itemFilter = _filterContext.checkValue(_itemFilter);
        if (_itemFilter == null) {
            _filterContext = _filterContext.createChildArrayContext(null, false);
            return;
        }
        if (_itemFilter != TokenFilter.INCLUDE_ALL) {
            _itemFilter = _itemFilter.filterStartArray();
        }
        if (_itemFilter == TokenFilter.INCLUDE_ALL) {
            _checkParentPath();
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, true);
            delegate.writeStartArray(forValue);
        } else if (_itemFilter != null && _inclusion == TokenFilter.Inclusion.INCLUDE_NON_NULL) {
            _checkParentPath(false);
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, true);
            delegate.writeStartArray(forValue);
        } else {
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, false);
        }
    }

    @Override
    public void writeStartArray(Object forValue, int size) throws IOException
    {
        if (_itemFilter == null) {
            _filterContext = _filterContext.createChildArrayContext(null, false);
            return;
        }
        if (_itemFilter == TokenFilter.INCLUDE_ALL) {
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, true);
            delegate.writeStartArray(forValue, size);
            return;
        }
        _itemFilter = _filterContext.checkValue(_itemFilter);
        if (_itemFilter == null) {
            _filterContext = _filterContext.createChildArrayContext(null, false);
            return;
        }
        if (_itemFilter != TokenFilter.INCLUDE_ALL) {
            _itemFilter = _itemFilter.filterStartArray();
        }
        if (_itemFilter == TokenFilter.INCLUDE_ALL) {
            _checkParentPath();
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, true);
            delegate.writeStartArray(forValue, size);
        } else if (_itemFilter != null && _inclusion == TokenFilter.Inclusion.INCLUDE_NON_NULL) {
            _checkParentPath(false);
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, true);
            delegate.writeStartArray(forValue, size);
        } else {
            _filterContext = _filterContext.createChildArrayContext(_itemFilter, false);
        }
    }
}
