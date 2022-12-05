package telran.java45.security.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import telran.java45.accounting.dao.UserAccountRepository;
import telran.java45.accounting.model.UserAccount;
import telran.java45.post.dao.PostRepository;
import telran.java45.post.model.Post;

@Component
@RequiredArgsConstructor
@Order(50)
public class DeletePostFilter implements Filter {

	final PostRepository postRepository;
	final UserAccountRepository userAccountRepository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		if (checkEndPoints(request.getServletPath(), request.getMethod())) {
			Principal principal = request.getUserPrincipal();
			String[] arr = path.split("/");
			String postId = arr[arr.length - 1];
			Post post = postRepository.findById(postId).orElse(null);
			if (post == null) {
				response.sendError(404, "post id = " + postId + " not found");
				return;
			}
			String author = post.getAuthor();
			UserAccount userAccount = userAccountRepository.findById(principal.getName()).get();
			if (!(principal.getName().equals(author) || userAccount.getRoles().contains("Moderator".toUpperCase()))) {
				response.sendError(403);
				return;
			}
		}
		chain.doFilter(request, response);

	}

	private boolean checkEndPoints(String method, String servletPath) {
		return servletPath.matches("/forum/post/\\w+/?") && "Delete".equalsIgnoreCase(method);

	}
}
