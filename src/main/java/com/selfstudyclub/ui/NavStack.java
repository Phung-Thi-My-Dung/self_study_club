package com.selfstudyclub.ui;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Simple navigation stack: Back button + ESC. */
public final class NavStack {
    private final Deque<JPanel> stack = new ArrayDeque<>();
    private final JPanel container;
    private final CardLayoutLike layout;

    public NavStack(JPanel container, CardLayoutLike layout) {
        this.container = Objects.requireNonNull(container);
        this.layout = Objects.requireNonNull(layout);
    }

    public void resetTo(JPanel root) {
        stack.clear();
        push(root);
    }

    public void push(JPanel panel) {
        stack.push(panel);
        layout.show(panel);
    }

    public boolean canBack() { return stack.size() > 1; }

    public void back() {
        if (stack.size() <= 1) return;
        stack.pop();
        layout.show(stack.peek());
    }

    public interface CardLayoutLike {
        void show(JPanel panel);
    }
}
