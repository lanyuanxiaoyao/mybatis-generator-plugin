package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;

import java.util.HashMap;
import java.util.Map;

public class ExampleMapEnhancedPlugin extends BasePlugin {

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        topLevelClass.getInnerClasses()
                .stream()
                .filter(innerClass -> "GeneratedCriteria".equalsIgnoreCase(innerClass.getType().getShortName()))
                .findFirst()
                .ifPresent(innerClass -> {
                    addModelEqualAndLikeEnhanced(topLevelClass, innerClass, introspectedTable);
                    addModelBetweenEnhanced(topLevelClass, innerClass, introspectedTable);
                });

        return true;
    }

    private void addModelEqualAndLikeEnhanced(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("=", "andMapEqualTo", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("<>", "andMapNotEqualTo", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("like", "andMapLike", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("not like", "andMapNotLike", introspectedTable));
    }

    private Method generateEqualAndLikeMethod(String keyword, String methodName, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(new FullyQualifiedJavaType("java.util.Map<String, String>"), "map")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        StringBuilder builder = new StringBuilder();
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String javaProperty = column.getJavaProperty();
                    builder.append("if (map.containsKey(\"").append(javaProperty).append("\")) {")
                            .append("String ").append(javaProperty).append(" = map.get(\"").append(javaProperty).append("\");")
                            .append("if (").append(javaProperty).append(" != null && !").append(javaProperty).append(".isEmpty()) {")
                            .append("addCriterion(\"").append(column.getActualColumnName()).append(" ").append(keyword).append(" \\\"\" + ").append(javaProperty).append(" + \"\\\"\");")
                            .append("}")
                            .append("}");
                });
        JavaElementGeneratorTools.generateMethodBody(
                method,
                builder.toString(),
                "return (Criteria) this;"
        );
        return method;
    }

    private void addModelBetweenEnhanced(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        FormatTools.addMethodWithBestPosition(innerClass, generateBetweenMethod("between", "andMapBetween", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateBetweenMethod("not between", "andMapNotBetween", introspectedTable));
    }

    private Method generateBetweenMethod(String keyword, String methodName, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(new FullyQualifiedJavaType("java.util.Map<String, String>"), "start"),
                new Parameter(new FullyQualifiedJavaType("java.util.Map<String, String>"), "end")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        StringBuilder builder = new StringBuilder();
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String startJavaProperty = column.getJavaProperty();
                    String endJavaProperty = column.getJavaProperty();
                    String startEscapeJavaProperty = "start" + generateCamel(startJavaProperty);
                    String endEscapeJavaProperty = "end" + generateCamel(endJavaProperty);
                    builder.append("if (start.containsKey(\"").append(startJavaProperty).append("\") && end.containsKey(\"").append(endJavaProperty).append("\")) {")
                            .append("String ").append(startEscapeJavaProperty).append(" = start.get(\"").append(startJavaProperty).append("\");")
                            .append("String ").append(endEscapeJavaProperty).append(" = end.get(\"").append(endJavaProperty).append("\");")
                            .append("if ((").append(startEscapeJavaProperty).append(" != null && !").append(startEscapeJavaProperty).append(".isEmpty()) && (").append(endEscapeJavaProperty).append(" != null && !").append(endEscapeJavaProperty).append(".isEmpty())) {")
                            .append("addCriterion(\"").append(column.getActualColumnName()).append(" between \\\"\" + ").append(startEscapeJavaProperty).append(" + \"\\\" and \\\"\" + ").append(endEscapeJavaProperty).append(" + \"\\\"\");")
                            .append("}")
                            .append("}");
                });
        JavaElementGeneratorTools.generateMethodBody(
                method,
                builder.toString(),
                "return (Criteria) this;"
        );
        return method;
    }

    private String generateCamel(String propertyName) {
        char[] chars = propertyName.toCharArray();
        chars[0] -= 32;
        return String.valueOf(chars);
    }
}
