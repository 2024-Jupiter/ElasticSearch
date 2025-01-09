
package com.myfeed.controller;
import com.myfeed.model.user.User;
import com.myfeed.service.Post.PostService;
import com.myfeed.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
        return "redirect:/api/admin/users/list";
    }
}
