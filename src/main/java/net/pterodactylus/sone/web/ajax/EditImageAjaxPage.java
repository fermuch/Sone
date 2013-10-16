/*
 * Sone - EditImageAjaxPage.java - Copyright © 2011–2013 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web.ajax;

import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.template.ParserFilter;
import net.pterodactylus.sone.text.TextFilter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Page that stores a user’s image modifications.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EditImageAjaxPage extends JsonPage {

	/** Parser for image descriptions. */
	private final ParserFilter parserFilter;

	/**
	 * Creates a new edit image AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 * @param parserFilter
	 *            The parser filter for image descriptions
	 */
	public EditImageAjaxPage(WebInterface webInterface, ParserFilter parserFilter) {
		super("editImage.ajax", webInterface);
		this.parserFilter = parserFilter;
	}

	//
	// JSONPAGE METHODS
	//

	@Override
	protected JsonReturnObject createJsonObject(FreenetRequest request) {
		String imageId = request.getHttpRequest().getParam("image");
		Optional<Image> image = webInterface.getCore().getImage(imageId);
		if (!image.isPresent()) {
			return createErrorJsonObject("invalid-image-id");
		}
		if (!image.get().getSone().isLocal()) {
			return createErrorJsonObject("not-authorized");
		}
		if ("true".equals(request.getHttpRequest().getParam("moveLeft"))) {
			image.get().moveUp();
			webInterface.getCore().touchConfiguration();
			return createSuccessJsonObject(); // TODO - fix javascript
		}
		if ("true".equals(request.getHttpRequest().getParam("moveRight"))) {
			image.get().moveDown();
			webInterface.getCore().touchConfiguration();
			return createSuccessJsonObject(); // TODO - fix javascript
		}
		String title = request.getHttpRequest().getParam("title").trim();
		String description = request.getHttpRequest().getParam("description").trim();
		image.get().modify().setTitle(title).setDescription(TextFilter.filter(request.getHttpRequest().getHeader("host"), description)).update();
		webInterface.getCore().touchConfiguration();
		return createSuccessJsonObject().put("imageId", image.get().getId()).put("title", image.get().getTitle()).put("description", image.get().getDescription()).put("parsedDescription", (String) parserFilter.format(new TemplateContext(), image.get().getDescription(), ImmutableMap.<String, Object>builder().put("sone", image.get().getSone()).build()));
	}

}
