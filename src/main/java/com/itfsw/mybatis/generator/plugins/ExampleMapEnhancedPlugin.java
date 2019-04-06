package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;

import java.util.ArrayList;
import java.util.List;

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

        List<String> bodyLine = new ArrayList<>();
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String javaProperty = column.getJavaProperty();
                    bodyLine.add("if (map.containsKey(\"" + javaProperty + "\")) {");
                    bodyLine.add("String " + javaProperty + " = map.get(\"" + javaProperty + "\");");
                    bodyLine.add("if (" + javaProperty + " != null && !" + javaProperty + ".isEmpty()) {");
                    bodyLine.add("addCriterion(\"" + column.getActualColumnName() + " " + keyword + " \\\"\" + " + javaProperty + " + \"\\\"\");");
                    bodyLine.add("}");
                    bodyLine.add("}");
                });
        bodyLine.add("return (Criteria) this;");
        method.addBodyLines(bodyLine);
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

        List<String> bodyLine = new ArrayList<>();
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String startJavaProperty = column.getJavaProperty();
                    String endJavaProperty = column.getJavaProperty();
                    String startEscapeJavaProperty = "start" + generateCamel(startJavaProperty);
                    String endEscapeJavaProperty = "end" + generateCamel(endJavaProperty);
                    bodyLine.add("if (start.containsKey(\"" + startJavaProperty + "\") && end.containsKey(\"" + endJavaProperty + "\")) {");
                    bodyLine.add("String " + startEscapeJavaProperty + " = start.get(\"" + startJavaProperty + "\");");
                    bodyLine.add("String " + endEscapeJavaProperty + " = end.get(\"" + endJavaProperty + "\");");
                    bodyLine.add("if ((" + startEscapeJavaProperty + " != null && !" + startEscapeJavaProperty + ".isEmpty()) && (" + endEscapeJavaProperty + " != null && !" + endEscapeJavaProperty + ".isEmpty())) {");
                    bodyLine.add("addCriterion(\"" + column.getActualColumnName() + " " + keyword + " \\\"\" + " + startEscapeJavaProperty + " + \"\\\" and \\\"\" + " + endEscapeJavaProperty + " + \"\\\"\");");
                    bodyLine.add("}");
                    bodyLine.add("}");
                });
        bodyLine.add("return (Criteria) this;");
        method.addBodyLines(bodyLine);
        return method;
    }

    private String generateCamel(String propertyName) {
        char[] chars = propertyName.toCharArray();
        chars[0] -= 32;
        return String.valueOf(chars);
    }
}
