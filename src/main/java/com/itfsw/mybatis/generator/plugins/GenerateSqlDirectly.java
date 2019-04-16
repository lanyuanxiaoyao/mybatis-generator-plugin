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
import java.util.stream.Collectors;

/**
 * @author ZhangJiacheng
 * @date 2019-04-14
 */
public class GenerateSqlDirectly extends BasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSqlDirectly.class);

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
                    generate(topLevelClass, innerClass, introspectedTable);
                });
        return true;
    }

    private void generate(TopLevelClass topLevelClass, InnerClass innerClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("java.util.List");
        topLevelClass.addImportedType("java.util.Map");
        Method method = JavaElementGeneratorTools.generateMethod(
                "andFilter",
                JavaVisibility.PUBLIC,
                FullyQualifiedJavaType.getCriteriaInstance(),
                new Parameter(new FullyQualifiedJavaType("List<Map<String, Object>>"), "filters")
        );
        commentGenerator.addGeneralMethodComment(method, introspectedTable);

        List<String> bodyLines = new ArrayList<>();
        bodyLines.add("for (Map<String, Object> filter : filters) {");
        bodyLines.add("Object value = filter.get(\"value\");");
        bodyLines.add("String field = (String) filter.get(\"field\");");
        bodyLines.add("field = getActualColumnName(field);");
        bodyLines.add("if(field.isEmpty()) {");
        bodyLines.add("continue;");
        bodyLines.add("}");
        bodyLines.add("String operator = (String) filter.get(\"operator\");");
        bodyLines.add("if (\"NULL\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" is null\");");
        bodyLines.add("}");
        bodyLines.add("else if (\"NOT_NULL\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" is not null\");");
        bodyLines.add("}");
        bodyLines.add("else if (\"EQUALS\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" =\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"NOT_EQUALS\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" <>\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"LIKE\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" like\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"NOT_LIKE\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" not like\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"GREATER\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" >\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"LESS\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" <\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"IN\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" in\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"NOT_IN\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("addCriterion(field + \" not in\", value, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"BETWEEN\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("Map<String, String> valueMap = (Map<String, String>) value;");
        bodyLines.add("String start = valueMap.get(\"start\");");
        bodyLines.add("String end = valueMap.get(\"end\");");
        bodyLines.add("addCriterion(field + \" between\", start, end, field);");
        bodyLines.add("}");
        bodyLines.add("else if (\"NOT_BETWEEN\".equalsIgnoreCase(operator)) { ");
        bodyLines.add("Map<String, String> valueMap = (Map<String, String>) value;");
        bodyLines.add("String start = valueMap.get(\"start\");");
        bodyLines.add("String end = valueMap.get(\"end\");");
        bodyLines.add("addCriterion(field + \" not between\", start, end, field);");
        bodyLines.add("}");
        bodyLines.add("}");
        bodyLines.add("return (Criteria) this;");

        method.addBodyLines(bodyLines);

        FormatTools.addMethodWithBestPosition(innerClass, method);
    }

}
