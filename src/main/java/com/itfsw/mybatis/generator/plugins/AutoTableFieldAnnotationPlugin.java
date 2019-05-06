package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 根据数据库约束条件自动加载实体类字段注解(@NotNull, @NotBlank, @Max, @Min)
 *
 * @author LanyuanXiaoyao
 * @date 2019-05-06
 */
public class AutoTableFieldAnnotationPlugin extends BasePlugin {

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        try {
            Map<String, IntrospectedColumn> columnMap = new HashMap<>();
            introspectedTable.getAllColumns()
                    .forEach(column -> columnMap.put(column.getJavaProperty(), column));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("javax.validation.constraints.*"));
            topLevelClass.getFields()
                    .stream()
                    .peek(field -> {
                        try {
                            IntrospectedColumn column = columnMap.get(field.getName());
                            if (!column.isAutoIncrement() && (!column.isNullable() || column.isIdentity())) {
                                if (column.isStringColumn()) {
                                    field.addAnnotation("@NotBlank");
                                } else {
                                    field.addAnnotation("@NotNull");
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    })
                    .forEach(field -> {
                        try {
                            IntrospectedColumn column = columnMap.get(field.getName());
                            String type = column.getFullyQualifiedJavaType().getShortName().toLowerCase();
                            if (Pattern.matches("long|integer|double|string", type)) {
                                if (!column.isNullable()) {
                                    field.addAnnotation("@Min(1)");
                                } else {
                                    field.addAnnotation("@Min(0)");
                                }
                                field.addAnnotation("@Max(" + column.getLength() + ")");
                            }
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception e) {
            topLevelClass.addAnnotation(e.getMessage());
        }
        return true;
    }
}
