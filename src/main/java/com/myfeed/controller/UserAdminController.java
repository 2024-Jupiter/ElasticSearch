
package com.myfeed.controller;

import com.myfeed.exception.CustomException;
import com.myfeed.model.post.Post;
import com.myfeed.model.user.LoginProvider;
import com.myfeed.model.user.RegisterDto;
import com.myfeed.model.user.Role;
import com.myfeed.model.user.UpdateDto;
import com.myfeed.model.user.User;
import com.myfeed.model.user.UserChangePasswordDto;
import com.myfeed.model.user.UserFindIdDto;
import com.myfeed.model.user.UserFindPasswordDto;
import com.myfeed.service.Post.PostService;
import com.myfeed.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/api/admin/users")
public class UserAdminController {
    @Autowired UserService userService;
    @Autowired PostService postService;

    // 활성/비활성 회원 목록 가져오기(admin만)
    @GetMapping("/list") //
    public String list(@RequestParam(name="p", defaultValue = "1") int page,
            @RequestParam(name="status", defaultValue = "true") boolean status,
            Model model) {
        Page<User> pagedUsers = userService.getPagedUser(page, status);
        model.addAttribute("pagedUsers", pagedUsers);
        model.addAttribute("status", status);
        model.addAttribute("currentUserPage", page);
        return "users/list";
    }

    //회원 활성/비활성 여부 수정하기(admin만)
    @PostMapping("/{uid}/status")
    public String updateUserState(@PathVariable Long id,
            @RequestParam(name="status") boolean status,
            Model model) {
        userService.updateUserStatus(id, status);
        return "redirect:/users/list";
    }
}
