package com.github.shiraji.createintentinspection.inspection;

import com.github.shiraji.createintentinspection.util.InspectionPsiUtil;
import com.intellij.codeInsight.daemon.impl.quickfix.AddMethodFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.siyeh.ig.BaseInspectionVisitor;
import org.jetbrains.annotations.NotNull;

class CreateIntentInspectionVisitor extends BaseInspectionVisitor {
    private static final String QUALIFIED_NAME_OF_SUPER_CLASS = "android.app.Activity";
    private static final String INTENT_CLASS_NAME = "Intent";
    private static final String INTENT_FULL_QUALIFIED_NAME = "android.content." + INTENT_CLASS_NAME;
    private static final String CONTENT_FULL_QUALIFIED_NAME = "android.content.Context";

    private ProblemsHolder mHolder;
    private String methodName;

    CreateIntentInspectionVisitor(ProblemsHolder holder, String name) {
        mHolder = holder;
        methodName = name;
    }

    @Override
    public void visitClass(PsiClass aClass) {
        if (InspectionPsiUtil.isAbstactClass(aClass)) {
            return;
        }

        if (!(aClass instanceof PsiClassImpl)) {
            return;
        }

        Project project = aClass.getProject();
        PsiClass activityClass = InspectionPsiUtil.createPsiClass(QUALIFIED_NAME_OF_SUPER_CLASS, project);
        if (activityClass == null || !aClass.isInheritor(activityClass,
                true)) {
            return;
        }

        PsiMethod[] methods = aClass.getMethods();
        for (PsiMethod method : methods) {
            if (isTargetMethod(method)) {
                return;
            }
        }

        registerProblem(aClass);
    }

    private boolean isTargetMethod(PsiMethod method) {
        return InspectionPsiUtil.isStaticMethod(method) &&
                InspectionPsiUtil.isPublicMethod(method) &&
                isValidMethodName(method) &&
                isReturnIntent(method);
    }

    private boolean isValidMethodName(PsiMethod method) {
        return methodName.equals(method.getName());
    }

    private boolean isReturnIntent(PsiMethod method) {
        return method.getReturnTypeElement() != null &&
                (INTENT_CLASS_NAME.equals(method.getReturnTypeElement().getText())
                        || INTENT_FULL_QUALIFIED_NAME.equals(method.getReturnTypeElement().getText()));
    }

    private void registerProblem(PsiClass aClass) {
        PsiElement nameIdentifier = aClass.getNameIdentifier();
        if (nameIdentifier == null) {
            nameIdentifier = aClass;
        }

        mHolder.registerProblem(nameIdentifier, "Implement public static Intent " + methodName + "(Context)",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                TextRange.allOf(aClass.getName()), new AddMethodFix(getMethodText(aClass), aClass));
    }

    @NotNull
    private String getMethodText(PsiClass aClass) {
        return "public static " + INTENT_FULL_QUALIFIED_NAME + " " + methodName + " (" + CONTENT_FULL_QUALIFIED_NAME + " context){ " +
                "Intent intent = new Intent(context, " + aClass.getName() + ".class);" +
                "return intent; " +
                "}";
    }
}
