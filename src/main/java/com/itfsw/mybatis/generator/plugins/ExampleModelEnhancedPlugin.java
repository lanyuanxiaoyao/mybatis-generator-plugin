package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;

public class ExampleModelEnhancedPlugin extends BasePlugin {

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
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("=", "andModelEqualTo", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("<>", "andModelNotEqualTo", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("like", "andModelLike", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("not like", "andModelNotLike", introspectedTable));
    }

    private Method generateEqualAndLikeMethod(String keyword, String methodName, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        StringBuilder builder = new StringBuilder();
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String javaProperty = "record." + generateGetter(column.getJavaProperty());
                    String escapeJavaProperty = javaProperty;
                    if (!"String".equalsIgnoreCase(column.getFullyQualifiedJavaType().getShortName())) {
                        escapeJavaProperty = "String.valueOf(" + javaProperty + ")";
                    }
                    builder.append("if (").append(javaProperty).append(" != null && !\"\".equalsIgnoreCase(").append(escapeJavaProperty).append(")) {")
                            .append("  addCriterion(\"").append(column.getActualColumnName()).append(" ").append(keyword).append(" \\\"\" + ").append(javaProperty).append(" + \"\\\"\");")
                            .append("}\n");
                });
        JavaElementGeneratorTools.generateMethodBody(
                method,
                builder.toString(),
                "return (Criteria) this;"
        );
        return method;
    }

    private void addModelBetweenEnhanced(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        FormatTools.addMethodWithBestPosition(innerClass, generateBetweenMethod("between", "andModelBetween", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateBetweenMethod("not between", "andModelNotBetween", introspectedTable));
    }

    private Method generateBetweenMethod(String keyword, String methodName, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "start"),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "end")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        StringBuilder builder = new StringBuilder();
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String startJavaProperty = "start." + generateGetter(column.getJavaProperty());
                    String endJavaProperty = "end." + generateGetter(column.getJavaProperty());
                    String startEscapeJavaProperty = startJavaProperty;
                    String endEscapeJavaProperty = endJavaProperty;
                    if (!"String".equalsIgnoreCase(column.getFullyQualifiedJavaType().getShortName())) {
                        startEscapeJavaProperty = "String.valueOf(" + startJavaProperty + ")";
                        endEscapeJavaProperty = "String.valueOf(" + endJavaProperty + ")";
                    }
                    builder.append("if ((").append(startJavaProperty).append(" != null && !\"\".equalsIgnoreCase(").append(startEscapeJavaProperty).append(")) && (")
                            .append(endJavaProperty).append(" != null && !\"\".equalsIgnoreCase(").append(endEscapeJavaProperty).append("))) {")
                            .append("  addCriterion(\"").append(column.getActualColumnName()).append(" ").append(keyword).append(" \" + ").append(startJavaProperty).append(" + \" and \" + ").append(endJavaProperty).append(");")
                            .append("}\n");
                });
        JavaElementGeneratorTools.generateMethodBody(
                method,
                builder.toString(),
                "return (Criteria) this;"
        );
        return method;
    }

    private String generateGetter(String propertyName) {
        char[] chars = propertyName.toCharArray();
        chars[0] -= 32;
        return "get" + String.valueOf(chars) + "()";
    }
}
