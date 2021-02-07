package com.herms.service;

import com.herms.entity.ResourceAttributeEntity;
import com.herms.entity.ResourceRowEntity;
import com.herms.entity.ResourceRowValueEntity;
import com.herms.enums.FieldType;
import com.herms.mapper.ResourceRowMapper;
import com.herms.mapper.ResourceRowValueMapper;
import com.herms.model.ResourceRowValue;
import com.herms.repository.ResourceRowValueRepository;
import com.herms.utils.ConvertUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ResourceRowValueService {
    @Inject
    private ResourceRowValueRepository repository;

    public ResourceRowValue create(ResourceRowValue model) {
        ResourceRowValueEntity entity = ResourceRowValueMapper.fromModel(model);
        repository.persist(entity);
        return ResourceRowValueMapper.toModel(entity);
    }

    public Map<String, Object> getRowValuesUsingResourceAttributes(ResourceRowEntity row,
                                                                    List<ResourceAttributeEntity> resourceAttributeList) {

        Map<String, ResourceAttributeEntity> attributeMetadata = new HashMap<>();
        for(ResourceAttributeEntity attribute : resourceAttributeList) {
            attributeMetadata.put(attribute.getFieldName(), attribute);
        }

        Map<String, Object> rowValues = new HashMap<>();
        for(ResourceRowValueEntity value : row.getValueList()) {
            rowValues.put(value.getField(), convertValueToType(value, attributeMetadata.get(value.getField())));
        }
        return rowValues;
    }

    private Object convertValueToType(ResourceRowValueEntity value, ResourceAttributeEntity attribute) {
        if (attribute.getFieldType() == FieldType.BOOLEAN) {
            return Boolean.valueOf(value.getValue());
        } else if (attribute.getFieldType() == FieldType.DATE) {
            Date date = ConvertUtils.stringToDate(value.getValue(), attribute.getFieldFormat());
            return ConvertUtils.dateToString(date, attribute.getFieldFormat());
        } else if (attribute.getFieldType() == FieldType.INTEGER) {
            return Integer.valueOf(value.getValue());
        } else if (attribute.getFieldType() == FieldType.DOUBLE) {
            return Double.valueOf(value.getValue());
        } else {
            return value.getValue();
        }
    }

    public void updateFieldValuesToNewFormat(ResourceAttributeEntity entity, String newFormat) {
        List<ResourceRowValueEntity> valueEntityList = repository.list(
                                                "row.resource.id = ?1 and field = ?2",
                                                        entity.getResource().getId(),
                                                        entity.getFieldName());
        for(ResourceRowValueEntity value : valueEntityList) {
            String newValue = "";
            if (entity.getFieldType() == FieldType.DATE) {
                Date date = ConvertUtils.stringToDate(value.getValue(), entity.getFieldFormat());
                newValue = ConvertUtils.dateToString(date, newFormat);
            }
            //TODO If needed, create the same logic for other fieldTypes. For now I don't think it's necessary.
            value.setValue(newValue);
        }

    }
}
