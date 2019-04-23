package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.BasePlugin;
import com.itfsw.mybatis.generator.plugins.utils.FormatTools;
import com.itfsw.mybatis.generator.plugins.utils.JavaElementGeneratorTools;
import com.itfsw.mybatis.generator.plugins.utils.PluginTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExampleFieldEnhancePlugin extends BasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(ExampleFieldEnhancePlugin.class);

    @Override
    public boolean validate(List<String> warnings) {
        if (!PluginTools.checkDependencyPlugin(getContext(), ExampleColumnPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ExampleColumnPlugin插件使用！");
            return false;
        }
        return super.validate(warnings);
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.getInnerClasses()
                .stream()
                .filter(innerClass -> "GeneratedCriteria".equalsIgnoreCase(innerClass.getType().getShortName()))
                .findFirst()
                .ifPresent(innerClass -> {
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldEqualTo", "=", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldNotEqualTo", "<>", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldLike", "like", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldNotLike", "not like", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldGreaterThan", ">", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldGreaterThanOrEqualTo", ">=", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldLessThan", "<", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateOneParamMethod("andFieldLessThanOrEqualTo", "<=", topLevelClass, innerClass, introspectedTable));

                    FormatTools.addMethodWithBestPosition(innerClass, generateTwoParamMethod("andFieldBetween", "between", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateTwoParamMethod("andFieldNotBetween", "not between", topLevelClass, innerClass, introspectedTable));

                    FormatTools.addMethodWithBestPosition(innerClass, generateListParamMethod("andFieldIn", "in", topLevelClass, innerClass, introspectedTable));
                    FormatTools.addMethodWithBestPosition(innerClass, generateListParamMethod("andFieldNotIn", "not in", topLevelClass, innerClass, introspectedTable));
                });
        return true;
    }

    private Method generateOneParamMethod(String methodName, String operator, TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "field"),
                new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("field = getActualColumnName(field);");
        bodyLines.add("if(!field.isEmpty()) {");
        bodyLines.add("addCriterion(field + \" " + operator + " \" + value);");
        bodyLines.add("}");
        bodyLines.add("return (Criteria) this;");

        method.addBodyLines(bodyLines);

        return method;
    }

    private Method generateTwoParamMethod(String methodName, String operator, TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "field"),
                new Parameter(FullyQualifiedJavaType.getObjectInstance(), "start"),
                new Parameter(FullyQualifiedJavaType.getObjectInstance(), "end")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("field = getActualColumnName(field);");
        bodyLines.add("if(!field.isEmpty()) {");
        bodyLines.add("addCriterion(field + \" " + operator + "\", start, end, field);");
        bodyLines.add("}");
        bodyLines.add("return (Criteria) this;");

        method.addBodyLines(bodyLines);

        return method;
    }

    private Method generateListParamMethod(String methodName, String operator, TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        Method method = JavaElementGeneratorTools.generateMethod(
                methodName,
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(FullyQualifiedJavaType.getStringInstance(), "field"),
                new Parameter(new FullyQualifiedJavaType("List<Object>"), "values")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("field = getActualColumnName(field);");
        bodyLines.add("if(!field.isEmpty()) {");
        bodyLines.add("addCriterion(field + \" " + operator + "\", values, field);");
        bodyLines.add("}");
        bodyLines.add("return (Criteria) this;");

        method.addBodyLines(bodyLines);

        return method;
    }

}
