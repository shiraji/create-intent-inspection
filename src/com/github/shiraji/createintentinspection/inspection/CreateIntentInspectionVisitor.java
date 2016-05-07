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

public class CreateIntentInspectionVisitor extends BaseInspectionVisitor {
    public static final String QUALIFIED_NAME_OF_SUPER_CLASS = "android.app.Activity";
    public static final String STATIC_METHOD_NAME = "createIntent";
    public static final String RETURN_CLASS_NAME = "Intent";
    public static final String RETURN_FULL_QUALIFIED_NAME = "android.content.Intent";
    public static final String ALERT_MESSAGE = "Implement public static Intent createIntent()";

    private ProblemsHolder mHolder;
    private boolean mIsOnTheFly;

    public CreateIntentInspectionVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        mHolder = holder;
        mIsOnTheFly = isOnTheFly;
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
            if (isCreateIntentMethod(method)) {
                return;
            }
        }

        registerProblem(aClass);
    }

    private boolean isCreateIntentMethod(PsiMethod method) {
        return InspectionPsiUtil.isStaticMethod(method) &&
                InspectionPsiUtil.isPublicMethod(method) &&
                isMethodNameCreateIntent(method) &&
                isReturnIntent(method);
    }

    private boolean isMethodNameCreateIntent(PsiMethod method) {
        return STATIC_METHOD_NAME.equals(method.getName());
    }

    private boolean isReturnIntent(PsiMethod method) {
        return method.getReturnTypeElement() != null &&
                (RETURN_CLASS_NAME.equals(method.getReturnTypeElement().getText())
                        || RETURN_FULL_QUALIFIED_NAME.equals(method.getReturnTypeElement().getText()));
    }

    private void registerProblem(PsiClass aClass) {
        PsiElement nameIdentifier = aClass.getNameIdentifier();
        if (nameIdentifier == null) {
            nameIdentifier = aClass;
        }

        mHolder.registerProblem(nameIdentifier, ALERT_MESSAGE,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                TextRange.allOf(aClass.getName()), new AddMethodFix(getMethodText(aClass), aClass));
    }

    @NotNull
    private String getMethodText(PsiClass aClass) {
        return "public static android.content.Intent " + STATIC_METHOD_NAME + "(android.content.Context context) { " +
                "Intent intent = new Intent(context, " + aClass.getName() + ".class);" +
                "return intent; " +
                "}";
    }
}
