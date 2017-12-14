package org.training.conv;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.training.constants.YcommercewebservicesConstants;

import java.util.Optional;

public class ImageUrlConverter implements SingleValueConverter
{
    @Override
    public String toString(Object o)
    {
        return Optional.ofNullable(o)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(this::addRootContext)
                .orElseGet(() -> null);
    }

    protected String addRootContext(final String imageUrl){
        return new StringBuilder(YcommercewebservicesConstants.V1_ROOT_CONTEXT)
                .append(imageUrl)
                .toString();
    }

    @Override
    public Object fromString(String s)
    {
        return null;
    }

    @Override
    public boolean canConvert(Class type)
    {
        return type == String.class;
    }
}
