package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据实体生成 Example 查询增强方法, 允许传入一个实体作为组合查询条件
 *
 * @author LanyuanXiaoyao
 * @date 2019-05-06
 */
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
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod(">", "andModelGreaterThan", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod(">=", "andModelGreaterThanOrEqualTo", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("<", "andModelLessThan", introspectedTable));
        FormatTools.addMethodWithBestPosition(innerClass, generateEqualAndLikeMethod("<=", "andModelLessThanOrEqualTo", introspectedTable));
    }

    private Method generateEqualAndLikeMethod(String keyword, String methodName, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("if (record != null) {");
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String javaProperty = "record." + generateGetter(column.getJavaProperty());
                    bodyLines.add("if (" + javaProperty + " != null) {");
                    bodyLines.add("addCriterion(\"" + column.getActualColumnName() + " " + keyword + "\", " + javaProperty + ", \"" + column.getActualColumnName() + "\");");
                    bodyLines.add("}");
                });
        bodyLines.add("}");
        bodyLines.add("return (Criteria) this;");
        method.addBodyLines(bodyLines);
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

        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("if (start != null && end != null) {");
        introspectedTable.getAllColumns()
                .forEach(column -> {
                    String startJavaProperty = "start." + generateGetter(column.getJavaProperty());
                    String endJavaProperty = "end." + generateGetter(column.getJavaProperty());
                    bodyLines.add("if (" + startJavaProperty + " != null && " + endJavaProperty + " != null) {");
                    bodyLines.add("addCriterion(\"" + column.getActualColumnName() + " " + keyword + "\", " + startJavaProperty + ", " + endJavaProperty + ", \"" + column.getActualColumnName() + "\");");
                    bodyLines.add("}");
                });
        bodyLines.add("}");
        bodyLines.add("return (Criteria) this;");
        method.addBodyLines(bodyLines);
        return method;
    }

    private String generateGetter(String propertyName) {
        char[] chars = propertyName.toCharArray();
        chars[0] -= 32;
        return "get" + String.valueOf(chars) + "()";
    }
}
