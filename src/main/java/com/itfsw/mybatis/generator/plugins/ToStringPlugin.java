package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.List;

public class ToStringPlugin extends BasePlugin {

    private static final String PROPERTY_NAME = "type";
    private static final String STRING_JOINER = "StringJoiner";

    private String type = STRING_JOINER;

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        if (introspectedTable.getTableConfigurationProperty(PROPERTY_NAME) != null) {
            type = this.properties.getProperty(PROPERTY_NAME);
        }
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.StringJoiner"));

        Method toStringMethod = JavaElementGeneratorTools.generateMethod(
                "toString",
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getStringInstance()
        );
        toStringMethod.addAnnotation("@Override");
        commentGenerator.addGeneralMethodComment(toStringMethod, introspectedTable);

        List<String> methodBody = new ArrayList<>();
        switch (type) {
            case STRING_JOINER:
                methodBody = generateStringJoiner(introspectedTable);
                break;
            default:
                methodBody.add("return super.toString();");
        }

        toStringMethod.addBodyLines(methodBody);

        FormatTools.addMethodWithBestPosition(topLevelClass, toStringMethod);

        return true;
    }

    public List<String> generateStringJoiner(IntrospectedTable introspectedTable) {
        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("return new StringJoiner(\", \", \"" + new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()).getShortName() + " [\", \"]\")");
        introspectedTable.getPrimaryKeyColumns()
                .forEach(column -> bodyLines.add(".add(\"" + column.getJavaProperty() + "=\" + " + generateGetter(column.getJavaProperty()) + ")"));
        introspectedTable.getNonPrimaryKeyColumns()
                .forEach(column -> bodyLines.add(".add(\"" + column.getJavaProperty() + "=\" + " + column.getJavaProperty() + ")"));
        bodyLines.add(".toString();");
        return bodyLines;
    }

    private String generateGetter(String propertyName) {
        return String.format(
                "get%s%s()",
                propertyName.substring(0, 1).toUpperCase(),
                propertyName.substring(1)
        );
    }

}
