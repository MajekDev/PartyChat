/*
 * This file is part of PartyChat, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 Majekdor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.majek.pc.gui;

import org.bukkit.Material;

/**
 * Handles toggles for an ItemStack in a gui.
 */
public class GuiToggle {

  private final String name;
  private final int hdbId;
  private final Material configMaterial;
  private final Material defaultMaterial;
  private final boolean visible;

  public GuiToggle(String name, int hdbId, Material configMaterial, Material defaultMaterial, boolean visible) {
    this.name = name;
    this.hdbId = hdbId;
    this.configMaterial = configMaterial;
    this.defaultMaterial = defaultMaterial;
    this.visible = visible;
  }

  public String name() {
    return name;
  }

  public boolean hasHdbId() {
    return hdbId != -1;
  }

  public int hdbId() {
    return hdbId;
  }

  public Material material() {
    return configMaterial == null ? defaultMaterial : configMaterial;
  }

  public boolean isVisible() {
    return visible;
  }
}