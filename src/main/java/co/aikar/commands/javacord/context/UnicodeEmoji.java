/*
 * Copyright (c) 2023 Kevin Zuman (Greenadine)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package co.aikar.commands.javacord.context;

import com.google.common.base.Preconditions;
import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.entity.emoji.Emoji;

import java.util.Optional;

/**
 * This class represents a Unicode {@link Emoji}.
 *
 * @since 0.3.0
 * @author Greenadine
 */
public class UnicodeEmoji implements Emoji {

    private final String unicodeEmoji;

    public UnicodeEmoji(String emoji) {
        Preconditions.checkArgument(EmojiManager.isEmoji(emoji), "The given string is not an emoji.");
        this.unicodeEmoji = emoji;
    }

    @Override
    public Optional<String> asUnicodeEmoji() {
        return Optional.of(unicodeEmoji);
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    public boolean isUnicodeEmoji() {
        return true;
    }

    public boolean isCustomEmoji() {
        return false;
    }

    @Override
    public String getMentionTag() {
        return unicodeEmoji;
    }
}
