package alicanteweb.pelisapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class TestController {

    @GetMapping("/users")
    @ResponseBody
    public String testUsers() {
        return "Test controller working";
    }

    @GetMapping("/admin-users")
    public String testAdminUsersPage() {
        return "admin/users-management";
    }
}
